import { NavLink } from 'react-router-dom'
import { Bell, PlusCircle, History } from 'lucide-react'
import { Clock } from './Clock.tsx'
import './Sidebar.css'

export function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar__brand">
        <Bell className="sidebar__brand-icon" aria-hidden="true" />
        <span className="sidebar__label">Broder</span>
      </div>

      <div className="sidebar__clock-wrap">
        <Clock />
      </div>

      <nav className="sidebar__nav">
        <ul className="sidebar__links">
          <li>
            <NavLink to="/" className="sidebar__link" end>
              <PlusCircle className="sidebar__link-icon" aria-hidden="true" />
              <span className="sidebar__label">Criar Alarme</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/alarms" className="sidebar__link">
              <Bell className="sidebar__link-icon" aria-hidden="true" />
              <span className="sidebar__label">Alarmes</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/history" className="sidebar__link">
              <History className="sidebar__link-icon" aria-hidden="true" />
              <span className="sidebar__label">Histórico</span>
            </NavLink>
          </li>
        </ul>
      </nav>
    </aside>
  )
}
