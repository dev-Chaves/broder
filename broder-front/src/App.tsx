import { RouterProvider } from 'react-router-dom'
import { router } from './router.tsx'
import { ErrorBoundary } from './shared/components/ErrorBoundary.tsx'

function App() {
  return (
    <ErrorBoundary>
      <RouterProvider router={router} />
    </ErrorBoundary>
  )
}

export default App
