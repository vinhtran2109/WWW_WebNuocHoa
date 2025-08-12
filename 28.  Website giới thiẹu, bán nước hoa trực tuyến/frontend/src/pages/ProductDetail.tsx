import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

type Product = { id:number; name:string; slug:string; description:string; priceCents:number; imageUrl?:string }

export default function ProductDetail(){
  const { slug } = useParams()
  const [product, setProduct] = useState<Product | null>(null)
  const [qty, setQty] = useState(1)
  useEffect(()=>{ fetch(`${API_URL}/api/products/${slug}`).then(async r=>{ if(r.ok) setProduct(await r.json()) }) },[slug])
  if(!product) return <p>Đang tải...</p>
  return (
    <div className="product-detail" style={{display:'grid',gridTemplateColumns:'300px 1fr',gap:24}}>
      <img src={product.imageUrl || '/images/placeholder.svg'} alt={product.name} />
      <div>
        <h1>{product.name}</h1>
        <p>{(product.priceCents/100).toLocaleString('vi-VN', { style:'currency', currency:'VND' })}</p>
        <p>{product.description}</p>
        <input type="number" min={1} value={qty} onChange={e=>setQty(parseInt(e.target.value)||1)} />
        <button onClick={()=>addToCart(product, qty)}>Thêm vào giỏ</button>
      </div>
    </div>
  )
}

function addToCart(p: Product, quantity:number){
  const cart = JSON.parse(localStorage.getItem('cart')||'{"items":[]}')
  const found = cart.items.find((it:any)=>it.productId===p.id)
  if(found) found.quantity += quantity; else cart.items.push({ productId:p.id, name:p.name, unitPrice:p.priceCents, quantity })
  localStorage.setItem('cart', JSON.stringify(cart))
  alert('Đã thêm vào giỏ')
}
