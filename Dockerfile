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

# ★このステージの最終成果物として、テスト済みのJARファイルを生成
# CIのテストが成功した後に本番イメージを作ることを想定し、ここでもテストをスキップ
RUN ./mvnw package -DskipTests


# =================================================================
# STAGE 2: 最終段階 (本番環境用の軽量イメージ)
# =================================================================
FROM eclipse-temurin:21-jdk-jammy

# 作業ディレクトリを設定
WORKDIR /app

# ★'builder'ステージから、作成されたJARファイルのみをコピー
COPY --from=builder /app/target/*.jar app.jar

# コンテナ起動時にアプリケーションを実行するコマンド
ENTRYPOINT ["java", "-jar", "app.jar"]