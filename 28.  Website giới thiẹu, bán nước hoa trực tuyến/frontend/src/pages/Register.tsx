import { useState } from 'react'
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function Register(){
  const [email, setEmail] = useState('')
  const [fullName, setFullName] = useState('')
  const [password, setPassword] = useState('')

  const onSubmit = async (e:React.FormEvent)=>{
    e.preventDefault()
    const res = await fetch(`${API_URL}/api/auth/register`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ email, fullName, password }) })
    if(res.ok){ alert('Đăng ký thành công, vui lòng đăng nhập'); window.location.href='/login' } else alert('Đăng ký thất bại')
  }

  return (
    <form onSubmit={onSubmit}>
      <h1>Đăng ký</h1>
      <input placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} />
      <input placeholder="Họ tên" value={fullName} onChange={e=>setFullName(e.target.value)} />
      <input placeholder="Mật khẩu" type="password" value={password} onChange={e=>setPassword(e.target.value)} />
      <button>Tạo tài khoản</button>
    </form>
  )
}
