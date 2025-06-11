cp -rf ./tests/checkstyle.xml ./checkstyle.xml
cp -rf ./tests/suppressions.xml ./suppressions.xml
cp -rf ./tests/gateway/Dockerfile ./gateway/Dockerfile
cp -rf ./tests/server/Dockerfile ./server/Dockerfile
cp -rf ./tests/docker-compose.yml ./docker-compose.yml
mvn enforcer:enforce -Denforcer.rules=requireProfileIdsExist -P check --no-transfer-progress &&
mvn verify -P check,coverage --no-transfer-progress &&
docker compose -f docker-compose.yml build