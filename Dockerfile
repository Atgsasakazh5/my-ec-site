# ベースとなるJava 21のJDKイメージを選択
FROM eclipse-temurin:21-jdk-jammy

# コンテナ内の作業ディレクトリを設定
WORKDIR /app

# Mavenのラッパーとpom.xmlを先にコピーする（依存関係のキャッシュを有効化するため）
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# まずは依存関係のみをダウンロードする
RUN ./mvnw dependency:go-offline

# ソースコードをコピーする
COPY src ./src

# デフォルトの実行コマンド（今回はテストで上書きするが、イメージの基本動作として定義）
CMD ["./mvnw", "spring-boot:run"]