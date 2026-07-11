import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  Users,
  ShieldCheck,
  Key,
  Building2,
  DollarSign,
  ChevronRight,
} from 'lucide-react'

const navItems = [
  {
    label: 'Dashboard',
    to: '/',
    icon: LayoutDashboard,
  },
  {
    label: 'User Management',
    to: '/users',
    icon: Users,
  },
  {
    label: 'Role Management',
    to: '/roles',
    icon: ShieldCheck,
  },
  {
    label: 'Permissions',
    to: '/permissions',
    icon: Key,
  },
  {
    label: 'Tenant Registrations',
    to: '/tenants',
    icon: Building2,
  },
  {
    label: 'Subscription Packages',
    to: '/tenant-fees',
    icon: DollarSign,
  },
]

export default function Sidebar({ isOpen, onClose }) {
  return (
    <>
      {/* Overlay for mobile */}
      {isOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/50 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed top-0 left-0 h-full z-30 w-64 bg-gray-900 text-white flex flex-col transition-transform duration-300 
          ${isOpen ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0 lg:static lg:z-auto`}
      >
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-4 border-b border-gray-700">
          <img
            src="/logo.jpeg"
            alt="MyOnline"
            className="w-10 h-10 rounded-lg object-contain bg-white flex-shrink-0"
          />
          <div>
            <p className="font-bold text-white text-base leading-tight">MyOnline</p>
            <p className="text-xs text-gray-400 leading-tight">System Admin Portal</p>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-4 overflow-y-auto">
          <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider px-3 mb-2">
            Main Menu
          </p>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={() => onClose?.()}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg mb-1 text-sm font-medium transition-all duration-150 group
                ${
                  isActive
                    ? 'bg-primary-600 text-white'
                    : 'text-gray-400 hover:bg-gray-800 hover:text-white'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <item.icon className={`w-4.5 h-4.5 flex-shrink-0 ${isActive ? 'text-white' : 'text-gray-500 group-hover:text-gray-300'}`} size={18} />
                  <span className="flex-1">{item.label}</span>
                  {isActive && <ChevronRight className="w-4 h-4 opacity-60" />}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* Version */}
        <div className="px-6 py-4 border-t border-gray-700">
          <p className="text-xs text-gray-500">Version 1.0.0</p>
        </div>
      </aside>
    </>
  )
}
