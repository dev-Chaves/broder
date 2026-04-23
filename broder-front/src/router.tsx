import { createBrowserRouter } from 'react-router-dom'
import { Layout } from './shared/components/Layout.tsx'
import BuilderPage from './features/alarmBuilder/pages/BuilderPage.tsx'
import AlarmsPage from './features/alarm/pages/AlarmsPage.tsx'
import HistoryPage from './features/history/pages/HistoryPage.tsx'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <BuilderPage /> },
      { path: 'alarms', element: <AlarmsPage /> },
      { path: 'history', element: <HistoryPage /> },
    ],
  },
])
