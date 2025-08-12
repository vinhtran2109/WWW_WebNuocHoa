import { useState } from 'react'
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export default function Login(){
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const onSubmit = async (e:React.FormEvent)=>{
    e.preventDefault()
    const res = await fetch(`${API_URL}/api/auth/login`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ email, password }) })
    if(res.ok){
      const data = await res.json();
      localStorage.setItem('token', data.token)
      localStorage.setItem('fullName', data.fullName)
      window.location.href = '/'
    } else alert('Đăng nhập thất bại')
  }

  return (
    <form onSubmit={onSubmit}>
      <h1>Đăng nhập</h1>
      <input placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} />
      <input placeholder="Mật khẩu" type="password" value={password} onChange={e=>setPassword(e.target.value)} />
      <button>Đăng nhập</button>
    </form>
  )
}
