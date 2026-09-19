const jwt = require('jsonwebtoken');
module.exports = (roles = []) => (req, res, next) => {
  const token = (req.headers.authorization || '').replace('Bearer ', '');
  if (!token) return res.status(401).json({ error: 'NO_TOKEN' });
  try {
    const payload = jwt.verify(token, process.env.JWT_SECRET);
    if (roles.length && !roles.includes(payload.role))
      return res.status(403).json({ error: 'FORBIDDEN' });
    req.user = payload;
    next();
  } catch {
    res.status(401).json({ error: 'INVALID_TOKEN' });
  }
};