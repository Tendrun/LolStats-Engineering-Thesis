import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import eslintPlugin from "@nabla/vite-plugin-eslint";
import tailwindcss from '@tailwindcss/vite'
import tsconfigPaths from 'vite-tsconfig-paths'


// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), eslintPlugin(), tailwindcss(), tsconfigPaths()],
  server: {
    port: 5174,
    strictPort: true,
  },
  preview: {
    port: 5174,
    strictPort: true,
  },
})