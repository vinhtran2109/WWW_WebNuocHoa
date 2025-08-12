import { Link, Outlet } from 'react-router-dom'
import './styles.css'

export default function App() {
  const token = localStorage.getItem('token')
  const userName = localStorage.getItem('fullName')
  return (
    <div>
      <header className="nav">
        <nav>
          <Link to="/">Trang chủ</Link>
          <Link to="/cart">Giỏ hàng</Link>
          {token ? (
            <span>Xin chào, {userName}</span>
          ) : (
            <>
              <Link to="/login">Đăng nhập</Link>
              <Link to="/register">Đăng ký</Link>
            </>
          )}
        </nav>
      </header>
      <main className="container">
        <Outlet />
      </main>
      <footer className="footer">© {new Date().getFullYear()} Perfume Store</footer>
    </div>
  )
}
