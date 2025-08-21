# =================================================================
# STAGE 1: ビルダー段階
# =================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# 依存関係のキャッシュ
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN ./mvnw dependency:go-offline

# ソースコードをコピー
COPY src ./src

RUN ./mvnw package -DskipTests


# =================================================================
# STAGE 2: 最終段階
# =================================================================
# より軽量なJREイメージを使用
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# builderステージから、作成されたJARファイルのみをコピー
COPY --from=builder /app/target/*.jar app.jar

# コンテナ起動時にアプリケーションを実行するコマンド
ENTRYPOINT ["java", "-jar", "app.jar"]