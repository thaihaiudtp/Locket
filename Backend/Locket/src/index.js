const express = require('express')
const connectDB = require('./config/db');
const route = require('./route/route');
const cors = require('cors');
const dotenv = require('dotenv');
dotenv.config();
// Connect to the database
connectDB();
const app = express()
const port = 3000
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.get('/', (req, res) => {
  res.send('Hello World!')
})
app.use(route)
app.use((err, req, res, next) => {
    console.error(`[ERROR] ${req.method} ${req.url}`);
    console.error(err.stack); 
    res.status(500).json({ error: 'Server error', message: err.message });
});

app.listen(port, () => {
  console.log(`Example app listening on port ${port}`)
})
