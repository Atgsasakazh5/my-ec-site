# =================================================================
# STAGE 1: ビルダー段階
# =================================================================
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu AS builder

ARG APP_JWT_SECRET
ARG STRIPE_API_KEY
# ビルド引数を環境変数として設定
ENV APP_JWT_SECRET=${APP_JWT_SECRET}
ENV STRIPE_SECRET_KEY=${STRIPE_API_KEY}

WORKDIR /app

# 依存関係のキャッシュ
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x ./mvnw
COPY pom.xml .
RUN ./mvnw dependency:go-offline

# ソースコードをコピー
COPY src ./src

# アプリケーションをビルドし、JARファイルを生成する
RUN ./mvnw package -DskipTests


# =================================================================
# STAGE 2: 最終段階
# =================================================================
# より軽量なJRE（Java Runtime Environment）イメージを使用
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

WORKDIR /app

# builderステージから、作成されたJARファイルのみをコピー
COPY --from=builder /app/target/*.jar app.jar

# コンテナ起動時にアプリケーションを実行するコマンド
ENTRYPOINT ["java", "-jar", "app.jar"]