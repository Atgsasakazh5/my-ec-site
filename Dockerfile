# =================================================================
# STAGE 1: ビルダー段階 (テスト実行とビルドの両方に利用)
# =================================================================
# 'builder' という名前をこのステージに付ける
FROM eclipse-temurin:21-jdk-jammy AS builder

# 作業ディレクトリを設定
WORKDIR /app

# Mavenのラッパーとpom.xmlを先にコピー
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# 依存関係をダウンロード
RUN ./mvnw dependency:go-offline

# ソースコードをコピー
COPY src ./src


# =================================================================
# STAGE 2: 最終段階 (本番環境用の軽量イメージ)
# =================================================================
FROM eclipse-temurin:21-jdk-jammy

# 作業ディレクトリを設定
WORKDIR /app

RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=bind,source=.,target=/app \
    ./mvnw package -DskipTests

# builderステージから、作成されたJARファイルのみをコピー
COPY --from=builder /app/target/*.jar app.jar

# コンテナ起動時にアプリケーションを実行するコマンド
ENTRYPOINT ["java", "-jar", "app.jar"]