import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar.tsx'
import './Layout.css'

export function Layout() {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
