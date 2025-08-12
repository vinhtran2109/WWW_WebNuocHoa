import { useEffect, useState } from 'react'
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

type Item = {productId:number; name:string; unitPrice:number; quantity:number}

type Cart = {items: Item[]}

export default function Cart(){
  const [cart, setCart] = useState<Cart>({items: []})
  useEffect(()=>{ setCart(JSON.parse(localStorage.getItem('cart')||'{"items":[]}')) },[])

  const updateQty = (pid:number, qty:number)=>{
    const items = cart.items.map(it=> it.productId===pid ? {...it, quantity: Math.max(0, qty)} : it).filter(it=>it.quantity>0)
    const next = { items }
    setCart(next); localStorage.setItem('cart', JSON.stringify(next))
  }

  const checkout = async ()=>{
    if(cart.items.length===0) return
    const token = localStorage.getItem('token')
    if(!token){ alert('Vui lòng đăng nhập trước khi thanh toán'); return }
    const res = await fetch(`${API_URL}/api/orders/checkout`, {
      method: 'POST', headers: { 'Content-Type':'application/json', 'Authorization': 'Bearer '+token },
      body: JSON.stringify({ items: cart.items.map(it=>({ productId: it.productId, quantity: it.quantity })) })
    })
    if(res.ok){
      const data = await res.json();
      alert('Đặt hàng thành công #'+data.orderId)
      localStorage.removeItem('cart'); setCart({items:[]})
    } else {
      alert('Thanh toán thất bại')
    }
  }

  const total = cart.items.reduce((s,it)=> s + it.unitPrice*it.quantity, 0)

  return (
    <div>
      <h1>Giỏ hàng</h1>
      {cart.items.length===0 ? <p>Giỏ hàng trống</p> : (
        <>
          <table>
            <thead><tr><th>Sản phẩm</th><th>Đơn giá</th><th>Số lượng</th><th>Tổng</th></tr></thead>
            <tbody>
              {cart.items.map(it=> (
                <tr key={it.productId}>
                  <td>{it.name}</td>
                  <td>{(it.unitPrice/100).toLocaleString('vi-VN', { style:'currency', currency:'VND' })}</td>
                  <td><input type="number" min={0} value={it.quantity} onChange={e=>updateQty(it.productId, parseInt(e.target.value)||0)} /></td>
                  <td>{((it.unitPrice*it.quantity)/100).toLocaleString('vi-VN', { style:'currency', currency:'VND' })}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p><strong>Tổng: {(total/100).toLocaleString('vi-VN', { style:'currency', currency:'VND' })}</strong></p>
          <button onClick={checkout}>Thanh toán</button>
        </>
      )}
    </div>
  )
}
