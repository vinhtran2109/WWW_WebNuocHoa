const nodemailer = require('nodemailer');

let transporter;
function getTransporter() {
  if (!transporter) {
    transporter = nodemailer.createTransport({
      host: process.env.MAIL_HOST || 'localhost',
      port: parseInt(process.env.MAIL_PORT || '1025', 10),
      secure: false,
      auth: process.env.MAIL_USER
        ? { user: process.env.MAIL_USER, pass: process.env.MAIL_PASS }
        : undefined,
    });
  }
  return transporter;
}

async function sendMailSafe({ to, subject, text, html }) {
  try {
    const info = await getTransporter().sendMail({ from: 'no-reply@perfume.local', to, subject, text, html });
    return info;
  } catch (err) {
    console.error('Mail error:', err.message);
    return null;
  }
}

module.exports = { sendMailSafe };
