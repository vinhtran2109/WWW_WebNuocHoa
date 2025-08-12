import React from 'react'
import ReactDOM from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import App from './pages/App'
import Products from './pages/Products'
import ProductDetail from './pages/ProductDetail'
import Cart from './pages/Cart'
import Login from './pages/Login'
import Register from './pages/Register'

const router = createBrowserRouter([
  { path: '/', element: <App />, children: [
    { index: true, element: <Products /> },
    { path: 'products/:slug', element: <ProductDetail /> },
    { path: 'cart', element: <Cart /> },
    { path: 'login', element: <Login /> },
    { path: 'register', element: <Register /> },
  ]},
])

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
)
