import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

type Product = { id:number; name:string; slug:string; priceCents:number; imageUrl?:string }

export default function Products(){
  const [products, setProducts] = useState<Product[]>([])
  const [q, setQ] = useState('')

  useEffect(()=>{ fetch(`${API_URL}/api/products`).then(r=>r.json()).then(setProducts) },[])
  const search = async (e:React.FormEvent)=>{ e.preventDefault(); const res = await fetch(`${API_URL}/api/products?q=`+encodeURIComponent(q)); setProducts(await res.json()) }

  return (
    <div>
      <h1>Sản phẩm</h1>
      <form onSubmit={search} style={{marginBottom:12}}>
        <input value={q} onChange={e=>setQ(e.target.value)} placeholder="Tìm kiếm..." />
        <button>Tìm</button>
      </form>
      <div className="grid">
        {products.map(p=> (
          <div className="card" key={p.id}>
            <Link to={`/products/${p.slug}`}>
              <img src={p.imageUrl || '/images/placeholder.svg'} alt={p.name} />
              <h3>{p.name}</h3>
              <p>{(p.priceCents/100).toLocaleString('vi-VN', { style:'currency', currency:'VND' })}</p>
            </Link>
            <button onClick={()=>addToCart(p)}>Thêm vào giỏ</button>
          </div>
        ))}
      </div>
    </div>
  )
}

function addToCart(p: Product){
  const cart = JSON.parse(localStorage.getItem('cart')||'{"items":[]}')
  const found = cart.items.find((it:any)=>it.productId===p.id)
  if(found) found.quantity += 1; else cart.items.push({ productId:p.id, name:p.name, unitPrice:p.priceCents, quantity:1 })
  localStorage.setItem('cart', JSON.stringify(cart))
  alert('Đã thêm vào giỏ')
}
