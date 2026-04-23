import { NavLink } from 'react-router-dom'
import { Bell, PlusCircle, History } from 'lucide-react'
import './Navbar.css'

export function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar__brand">
        <Bell className="navbar__icon" aria-hidden="true" />
        <span className="navbar__title">Broder</span>
      </div>
      <ul className="navbar__links">
        <li>
          <NavLink to="/" className="navbar__link" end>
            <PlusCircle className="navbar__link-icon" aria-hidden="true" />
            <span>Criar Alarme</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/alarms" className="navbar__link">
            <Bell className="navbar__link-icon" aria-hidden="true" />
            <span>Alarmes</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/history" className="navbar__link">
            <History className="navbar__link-icon" aria-hidden="true" />
            <span>Histórico</span>
          </NavLink>
        </li>
      </ul>
    </nav>
  )
}
