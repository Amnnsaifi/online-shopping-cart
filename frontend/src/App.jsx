import { useEffect, useState } from 'react'
import './App.css'

const API_BASE_URL = 'http://localhost:8080/api/v1'

function App() {
  const [products, setProducts] = useState([])
  const [cart, setCart] = useState([])
  const [token, setToken] = useState(localStorage.getItem('token') || '')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  async function login() {
    try {
      setLoading(true)
      setMessage('')

      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          password: password,
        }),
      })

      const data = await response.text()

      if (!response.ok) {
        setMessage('Login failed: Invalid email or password')
        return
      }

      localStorage.setItem('token', data)
      setToken(data)
      setMessage('Login successful')
    } catch (error) {
      setMessage('Backend connection failed')
    } finally {
      setLoading(false)
    }
  }

  function logout() {
    localStorage.removeItem('token')
    setToken('')
    setCart([])
    setProducts([])
    setMessage('Logged out')
  }

  async function loadProducts() {
    try {
      const response = await fetch(`${API_BASE_URL}/products`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        setMessage('Could not load products')
        return
      }

      const data = await response.json()
      setProducts(data)
    } catch (error) {
      setMessage('Could not connect to backend')
    }
  }

  async function loadCart() {
    if (!token) {
      return
    }

    try {
      const response = await fetch(`${API_BASE_URL}/cart`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        return
      }

      const data = await response.json()
      setCart(data)
    } catch (error) {
      console.error(error)
    }
  }

  async function addToCart(productId) {
    try {
      const response = await fetch(`${API_BASE_URL}/cart`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          productId: productId,
          quantity: 1,
        }),
      })

      const data = await response.json()

      if (!response.ok) {
        setMessage(data.message || 'Could not add product to cart')
        return
      }

      setMessage('Product added to cart')
      await loadCart()
    } catch (error) {
      setMessage('Could not connect to backend')
    }
  }

  async function checkout() {
    try {
      const response = await fetch(`${API_BASE_URL}/orders`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      const data = await response.json()

      if (!response.ok) {
        setMessage(data.message || 'Checkout failed')
        return
      }

      setMessage(
        `Order #${data.id} placed successfully. Total: ₹${data.totalAmount}`
      )

      setCart([])
      await loadProducts()
    } catch (error) {
      setMessage('Checkout failed')
    }
  }

  useEffect(() => {
    if (!token) {
      return
    }

    loadProducts()
    loadCart()
  }, [token])

  const cartTotal = cart.reduce((total, item) => {
    const product = products.find(
      (currentProduct) => currentProduct.id === item.productId
    )

    if (!product) {
      return total
    }

    return total + product.price * item.quantity
  }, 0)

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Online Shopping Cart</h1>
          <p>React + Spring Boot</p>
        </div>

        {token && (
          <button className="logout-button" onClick={logout}>
            Logout
          </button>
        )}
      </header>

      {!token ? (
        <section className="login-card">
          <h2>Login</h2>

          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />

          <button onClick={login} disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>

          <p className="demo-text">
            Customer login: customer@example.com / Customer@12345
          </p>

          {message && <p className="message">{message}</p>}
        </section>
      ) : (
        <>
          <main className="content">
            <section className="products-section">
              <div className="section-heading">
                <h2>Products</h2>

                <button onClick={loadProducts}>
                  Refresh
                </button>
              </div>

              <div className="product-grid">
                {products.map((product) => (
                  <div className="product-card" key={product.id}>
                    <div className="product-icon">🛍️</div>

                    <h3>{product.name}</h3>

                    <p>{product.description}</p>

                    <div className="product-info">
                      <strong>₹{product.price}</strong>
                      <span>Stock: {product.quantity}</span>
                    </div>

                    <button
                      onClick={() => addToCart(product.id)}
                      disabled={product.quantity <= 0}
                    >
                      {product.quantity > 0
                        ? 'Add to Cart'
                        : 'Out of Stock'}
                    </button>
                  </div>
                ))}
              </div>
            </section>

            <aside className="cart-section">
              <div className="section-heading">
                <h2>Cart</h2>
                <span>{cart.length} item(s)</span>
              </div>

              {cart.length === 0 ? (
                <p className="empty-cart">
                  Your cart is empty.
                </p>
              ) : (
                <>
                  <div className="cart-list">
                    {cart.map((item) => {
                      const product = products.find(
                        (currentProduct) =>
                          currentProduct.id === item.productId
                      )

                      return (
                        <div className="cart-item" key={item.id}>
                          <div>
                            <h3>
                              {product
                                ? product.name
                                : `Product #${item.productId}`}
                            </h3>

                            <p>
                              Quantity: {item.quantity}
                            </p>
                          </div>

                          <strong>
                            ₹
                            {product
                              ? product.price * item.quantity
                              : 0}
                          </strong>
                        </div>
                      )
                    })}
                  </div>

                  <div className="cart-total">
                    <span>Total</span>
                    <strong>₹{cartTotal}</strong>
                  </div>

                  <button
                    className="checkout-button"
                    onClick={checkout}
                  >
                    Checkout
                  </button>
                </>
              )}
            </aside>
          </main>

          {message && (
            <div className="bottom-message">
              {message}
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default App