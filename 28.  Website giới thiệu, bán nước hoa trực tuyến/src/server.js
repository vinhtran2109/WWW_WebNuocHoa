require('dotenv').config();
const path = require('path');
const express = require('express');
const session = require('express-session');
const cookieParser = require('cookie-parser');
const methodOverride = require('method-override');
const morgan = require('morgan');
const helmet = require('helmet');
const csrf = require('csurf');
const { PrismaClient } = require('@prisma/client');
const { sendMailSafe } = require('./utils/mailer');

const app = express();
const prisma = new PrismaClient();

const PORT = process.env.APP_PORT || 3000;
const HOST = process.env.APP_HOST || '0.0.0.0';

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, '..', 'views'));

app.use(helmet());
app.use(morgan('dev'));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(cookieParser());
app.use(methodOverride('_method'));
app.use(express.static(path.join(__dirname, '..', 'public')));

app.use(
  session({
    name: 'sid',
    secret: process.env.SESSION_SECRET || 'dev',
    resave: false,
    saveUninitialized: false,
    cookie: { httpOnly: true, maxAge: 1000 * 60 * 60 * 2 },
  })
);

// CSRF for non-API routes
app.use(csrf());
app.use((req, res, next) => {
  res.locals.csrfToken = req.csrfToken();
  res.locals.session = req.session;
  next();
});

function ensureAuthenticated(req, res, next) {
  if (req.session.user) return next();
  res.redirect('/login');
}

function ensureAdmin(req, res, next) {
  if (req.session.user && req.session.user.role === 'ADMIN') return next();
  res.status(403).send('Forbidden');
}

// Simple cart helpers in session
function getCart(req) {
  if (!req.session.cart) req.session.cart = { items: [], totalCents: 0 };
  return req.session.cart;
}

function recalcCart(cart) {
  cart.totalCents = cart.items.reduce((sum, it) => sum + it.unitPrice * it.quantity, 0);
}

// Routes
app.get('/', async (req, res) => {
  const categories = await prisma.category.findMany({ orderBy: { name: 'asc' } });
  const products = await prisma.product.findMany({ take: 12, orderBy: { createdAt: 'desc' } });
  res.render('home', { categories, products });
});

app.get('/products', async (req, res) => {
  const { q, category } = req.query;
  const where = {};
  if (q) where.OR = [{ name: { contains: q, mode: 'insensitive' } }, { description: { contains: q, mode: 'insensitive' } }];
  if (category) where.category = { slug: category };
  const products = await prisma.product.findMany({ where, include: { category: true }, orderBy: { createdAt: 'desc' } });
  const categories = await prisma.category.findMany({ orderBy: { name: 'asc' } });
  res.render('products/list', { products, categories, q });
});

app.get('/products/:slug', async (req, res) => {
  const product = await prisma.product.findUnique({ where: { slug: req.params.slug }, include: { category: true } });
  if (!product) return res.status(404).send('Not found');
  res.render('products/detail', { product });
});

app.post('/cart/add/:id', async (req, res) => {
  const productId = parseInt(req.params.id, 10);
  const product = await prisma.product.findUnique({ where: { id: productId } });
  if (!product) return res.status(404).send('Not found');
  const quantity = Math.max(1, parseInt(req.body.quantity || '1', 10));
  const cart = getCart(req);
  const existing = cart.items.find((it) => it.productId === product.id);
  if (existing) {
    existing.quantity += quantity;
  } else {
    cart.items.push({ productId: product.id, name: product.name, unitPrice: product.price, quantity });
  }
  recalcCart(cart);
  res.redirect('/cart');
});

app.get('/cart', async (req, res) => {
  const cart = getCart(req);
  res.render('cart/view', { cart });
});

app.post('/cart/update', (req, res) => {
  const cart = getCart(req);
  const updates = req.body; // { productId: quantity }
  cart.items = cart.items
    .map((it) => {
      const newQtyStr = updates[String(it.productId)];
      const newQty = parseInt(newQtyStr, 10);
      return Number.isNaN(newQty) ? it : { ...it, quantity: newQty };
    })
    .filter((it) => it.quantity > 0);
  recalcCart(cart);
  res.redirect('/cart');
});

// Auth
const bcrypt = require('bcryptjs');
const { z } = require('zod');

app.get('/register', (req, res) => {
  res.render('auth/register');
});

app.post('/register', async (req, res) => {
  const schema = z.object({
    email: z.string().email(),
    password: z.string().min(6),
    fullName: z.string().min(1),
  });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success) return res.status(400).render('auth/register', { error: 'Dữ liệu không hợp lệ' });
  const { email, password, fullName } = parsed.data;
  const exists = await prisma.user.findUnique({ where: { email } });
  if (exists) return res.status(400).render('auth/register', { error: 'Email đã tồn tại' });
  const passwordHash = await bcrypt.hash(password, 10);
  const user = await prisma.user.create({ data: { email, passwordHash, fullName } });
  req.session.user = { id: user.id, email: user.email, fullName: user.fullName, role: user.role };
  // send welcome email (non-blocking)
  sendMailSafe({
    to: user.email,
    subject: 'Chào mừng đến Perfume Store',
    text: `Xin chào ${user.fullName},\n\nBạn đã đăng ký tài khoản thành công.\n\nTrân trọng,\nPerfume Store`,
  });
  res.redirect('/');
});

app.get('/login', (req, res) => {
  res.render('auth/login');
});

app.post('/login', async (req, res) => {
  const { email, password } = req.body;
  const user = await prisma.user.findUnique({ where: { email } });
  if (!user) return res.status(400).render('auth/login', { error: 'Sai thông tin đăng nhập' });
  const ok = await bcrypt.compare(password, user.passwordHash);
  if (!ok) return res.status(400).render('auth/login', { error: 'Sai thông tin đăng nhập' });
  req.session.user = { id: user.id, email: user.email, fullName: user.fullName, role: user.role };
  res.redirect('/');
});

app.post('/logout', (req, res) => {
  req.session.destroy(() => {
    res.redirect('/');
  });
});

// Checkout
app.post('/checkout', ensureAuthenticated, async (req, res) => {
  const cart = getCart(req);
  if (!cart.items.length) return res.status(400).redirect('/cart');
  const user = req.session.user;
  const order = await prisma.$transaction(async (tx) => {
    const order = await tx.order.create({
      data: {
        userId: user.id,
        totalCents: cart.totalCents,
        status: 'PAID',
        items: {
          create: cart.items.map((it) => ({ productId: it.productId, quantity: it.quantity, unitPrice: it.unitPrice })),
        },
      },
      include: { items: true },
    });
    return order;
  });
  req.session.cart = null;
  // send order email (non-blocking)
  sendMailSafe({
    to: user.email,
    subject: `Xác nhận đơn hàng #${order.id}`,
    text: `Đơn hàng #${order.id} đã được xác nhận. Tổng tiền: ${(order.totalCents/100).toLocaleString('vi-VN')} VND.`,
  });
  res.render('order/success', { order });
});

// Admin minimal routes
app.get('/admin', ensureAdmin, async (req, res) => {
  const counts = await Promise.all([
    prisma.user.count(),
    prisma.product.count(),
    prisma.category.count(),
    prisma.order.count(),
  ]);
  res.render('admin/dashboard', { counts: { users: counts[0], products: counts[1], categories: counts[2], orders: counts[3] } });
});

app.get('/admin/products', ensureAdmin, async (req, res) => {
  const products = await prisma.product.findMany({ include: { category: true } });
  res.render('admin/products/list', { products });
});

// basic create/edit/delete can be added incrementally

app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).send('Internal Server Error');
});

app.listen(PORT, HOST, () => {
  console.log(`Server listening at http://${HOST}:${PORT}`);
});
