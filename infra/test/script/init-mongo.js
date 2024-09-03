print('MongoDB 초기화 시작 #################################################################');
db = db.getSiblingDB('shopping-mall');
db.createUser(
    {
        user: 'user',
        pwd: 'pwd',
        roles: [{ role: 'readWrite', db: 'shopping-mall' }],
    },
);
db.createCollection('baseCollection');
print('MongoDB 초기화 - 계정생성 완료 #################################################################');
print('MongoDB 초기화 종료 #################################################################');

