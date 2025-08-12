const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');
const prisma = new PrismaClient();

async function main() {
  const catPerfume = await prisma.category.upsert({
    where: { slug: 'nuoc-hoa' },
    update: {},
    create: { name: 'Nước hoa', slug: 'nuoc-hoa' },
  });

  const products = [
    { name: 'Perfume Aurora', slug: 'perfume-aurora', price: 8900000, description: 'Hương thơm quyến rũ, lưu hương lâu', imageUrl: '/img/placeholder.png', stock: 50, categoryId: catPerfume.id },
    { name: 'Perfume Noir', slug: 'perfume-noir', price: 6500000, description: 'Hương trầm ấm, bí ẩn', imageUrl: '/img/placeholder.png', stock: 30, categoryId: catPerfume.id },
    { name: 'Perfume Blossom', slug: 'perfume-blossom', price: 4200000, description: 'Hương hoa cỏ tươi mát', imageUrl: '/img/placeholder.png', stock: 80, categoryId: catPerfume.id },
  ];

  for (const p of products) {
    await prisma.product.upsert({
      where: { slug: p.slug },
      update: {},
      create: p,
    });
  }

  // admin user
  const adminEmail = 'admin@example.com';
  const passwordHash = await bcrypt.hash('admin123', 10);
  await prisma.user.upsert({
    where: { email: adminEmail },
    update: { role: 'ADMIN' },
    create: { email: adminEmail, fullName: 'Site Admin', passwordHash, role: 'ADMIN' },
  });

  console.log('Seed done');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
}).finally(async () => {
  await prisma.$disconnect();
});
