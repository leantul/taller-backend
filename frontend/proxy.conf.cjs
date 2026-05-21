const target = 'https://taller-backend-iwyn.onrender.com';

function normalizeOrigin(proxyReq) {
  proxyReq.removeHeader('origin');
  proxyReq.removeHeader('Origin');
  proxyReq.setHeader('Origin', 'http://localhost:4200');
  proxyReq.setHeader('Host', 'taller-backend-iwyn.onrender.com');
}

function route() {
  return {
    target,
    secure: true,
    changeOrigin: true,
    logLevel: 'debug',
    onProxyReq: normalizeOrigin,
    on: {
      proxyReq: normalizeOrigin
    }
  };
}

module.exports = {
  '/auth': route(),
  '/client': route(),
  '/device': route(),
  '/repair': route(),
  '/dashboard': route(),
  '/finance': route(),
  '/notifications': route(),
  '/common': route()
};
