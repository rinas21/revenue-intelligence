import React from 'react'
import { useState } from 'react'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import './App.css'

function OrderForm() {
  const [form, setForm] = useState({
    customerId: '',
    productId: '',
    quantity: 1,
    price: 0,
    discount: 0,
  })

  const [errors, setErrors] = useState<{ [key: string]: string }>({})
  const [submitting, setSubmitting] = useState(false)
  const [success, setSuccess] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    const numValue = name === 'quantity' || name === 'price' || name === 'discount'
      ? Math.max(0, parseInt(value) || 0)
      : value
    setForm(prev => ({ ...prev, [name]: numValue }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setErrors({})

    try {
      const response = await fetch('/api/v1/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          customerId: form.customerId,
          productId: form.productId,
          quantity: form.quantity,
          price: form.price,
          discount: form.discount,
        }),
      })

      if (response.ok) {
        setSuccess(true)
        setSubmitting(false)
        setTimeout(() => {
          setSuccess(false)
          setSubmitting(false)
        }, 3000)
      } else {
        const errorData = await response.json().catch(() => ({}))
        setErrors(errorData && errorData.message ? { message: errorData.message } : { message: 'Request failed' })
        setSubmitting(false)
      }
    } catch {
      setErrors({ message: 'Network error' })
      setSubmitting(false)
    }
  }

  return (
    <section id="center">
      <div style={{ background: 'var(--hero-background, #1a1a2e)', color: 'var(--hero-text, #fafafa)', padding: '2rem' }}>
        <h2 style={{ margin: '0 0 1rem 0', fontSize: '1.25rem' }}>Record Sale</h2>

        {success && (
          <div style={{ color: 'green', marginBottom: '1rem' }}>
            Sale recorded successfully!
          </div>
        )}

        {submitting && (
          <div>Submitting...</div>
        )}

        {errors && Object.keys(errors).length > 0 && (
          <div style={{ color: 'red', marginBottom: '1rem' }}>
            <p>Please fix the following errors:</p>
            <ul>
              {Object.entries(errors).map(([key, message]) => React.createElement('li', { key: key }, message))}
            </ul>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ maxWidth: '400px', margin: '0 auto' }}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Customer ID</label>
            <input
              type="text"
              name="customerId"
              value={form.customerId}
              onChange={handleChange}
              placeholder="e.g., customer-uuid"
              required
            />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Product ID</label>
            <input
              type="text"
              name="productId"
              value={form.productId}
              onChange={handleChange}
              placeholder="e.g., product-uuid"
              required
            />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Quantity</label>
            <input
              type="number"
              name="quantity"
              value={form.quantity}
              onChange={handleChange}
              min="1"
              required
            />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Price (cents)</label>
            <input
              type="number"
              name="price"
              value={form.price}
              onChange={handleChange}
              min="0"
              required
            />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Discount (cents)</label>
            <input
              type="number"
              name="discount"
              value={form.discount}
              onChange={handleChange}
              min="0"
              required
            />
          </div>

          <button type="submit" disabled={submitting}>Record Sale</button>
        </form>
      </div>
    </section>
  )
}

function App() {
  return (
    <BrowserRouter>
      <nav style={{ marginBottom: '2rem', fontFamily: 'system-ui, sans-serif' }}>
        <Link to="/" style={{ marginRight: '1rem' }}>Home</Link>
        <Link to="/orders/new">Record Sale</Link>
      </nav>

      <Routes>
        <Route path="/" >
          <h1>Small Business Revenue Intelligence</h1>
          <p>
            Phase 1 &mdash; Foundation. The Spring Boot API and local infrastructure
            are running. Sale recording begins in Phase 3.
          </p>
        </Route>
        <Route path="/orders/new" element={<OrderForm />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App