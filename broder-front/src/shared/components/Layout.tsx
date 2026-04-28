import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar.tsx'
import './Layout.css'

export function Layout() {
  return (
    <div className="app-shell">
      <Sidebar />
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
