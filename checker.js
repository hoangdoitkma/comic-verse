const mysql = require('mysql2/promise');

const mappedTables = [
  'vip_packages', 'users', 'upload_logs', 'transactions', 'tags', 'system_configs',
  'subscriptions', 'search_history', 'reports', 'recommendations', 'reading_history',
  'ratings', 'notification_preferences', 'notifications', 'genres', 'follows',
  'comments', 'comic_views', 'comic_tags', 'comic_genres', 'comic_daily_stats',
  'comic_ai_summaries', 'comics', 'chapter_pages', 'chapter_likes',
  'chapter_ai_summaries', 'chapters', 'bookmarks', 'authors', 'age_ratings',
  'roles', 'user_roles' // adding known potential join tables or standard spring security tables
];

async function checkTables() {
  const connection = await mysql.createConnection({
    host: 'comic-verse-db.cniko6wq6psw.ap-southeast-1.rds.amazonaws.com',
    user: 'admin',
    password: 'Duchoang003',
    database: 'comicverse'
  });

  const [rows] = await connection.execute('SHOW TABLES');
  const allTables = rows.map(r => Object.values(r)[0]);
  
  const redundantTables = allTables.filter(t => !mappedTables.includes(t));
  console.log('REDUNDANT_TABLES=' + JSON.stringify(redundantTables));
  
  await connection.end();
}

checkTables().catch(console.error);
