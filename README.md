# ECサイト バックエンドAPI

Spring BootとMySQLで構築した、基本的な機能を持つECサイトのバックエンドAPIです。

## 概要

このプロジェクトは、Webアプリケーション開発で求められる基本的な技術要素（認証、商品管理、在庫管理、カート、注文処理など）を網羅的に実装したECサイトのバックエンドです。

フロントエンド（Next.jsを想定）と連携し、ユーザーが商品を閲覧・購入できるオンラインストアの基盤を提供します。

-----

## 設計図

![システム構成図](https://www.plantuml.com/plantuml/svg/TLDTJnD157tVNp7P5-41UE3DH4B16aqqGPR45r-6tR5iR7URdLqJD2JTcV21IY0Y9T54RKK-4Ag481KC_J6NtRR_mfdP5Wg5JJFTtNlEFNVEcHscFlRy89T5wL5as3v1AS-S4l-c7tW4zQ6ngMbgcpYhQNVGFNKXO6LWlu3LWF-3i19LFjNFTbf53Pk-wo7zl5feBEm1tvGOzaUiEi8K1PHuRGYm8s0Rm1VgbL9zPQvHc5KGRELifqCjzSoGQdm7c6YsMF_mEohpBbapwmOMqWLOHsyq92xJpULTXpY-U9l3hrq7fL-9fn7FjZBakTDZc5F42hB4wrMojFseARRZMaIEU0XS_EQ0luqgNnkbzucyOaY1m2SjkMkIHjwpdGmQSLqVfV9veIT6atTeF70Sug41z01x6UoJH7uIco-9OwbMCsfLYuLzF8afkTZ3-7r3z7eX_7ioRJnzd90zYzwCJcSnzUrHBFRMOeqJowO9YwYYAOkAJKKgE7D9DWO59rgRYrR3-iVJY2z3k0lXJikkGMsc8wHeezZasp9H4JTV1VRtjeIwaJS4PlYUdIU9aBgz3f1c6caZGH0gPkaOfbaidzTONDZgca4UFzJNDtIHtLsaFvoO63DqJHsxMsfQAntKtpyapPObuFFgQDU4I6Dt7y9Q_BaAuPBU2UxqM97OAN0Ex1ZubZXOCP-DlXqC0Di7_hrT-76r0nsKn7O05M1b2UJhmFULWui3qN8nEbllc59k8h5DKkE3axYw4CrlYcyCzQJ67lNgLv64AjZXTboShvTF6hjBm4kgzyyayJPBk_X6q5MRWA-eeS94HJfoT1ozMnFdfrauYFTAuirbJ_u5s1wmgZ2WERjqNYl9rjgmc2V8PVy3)

![ER図](https://www.plantuml.com/plantuml/svg/fLRTRjn45BxVfnYr5sqXaHgat5HLr2Kni4hpGtuG4aAMinub1jlZZiUKq21XcwIB8b15rG0jeaHKPKlQf2XHAIYa3pFnRdBLL-ZCx3WxsOmRgTuBh-NpVyxCzvqh8R4mYJmN6CDWsY4G65CZbSevaDtRfl7cmSDL6jzltzkaoO-jEpiqRj1a5OIVEtvWOSkh7Bq1r_4XMGmWG9Wi84NW8H_L5p3o820uWfK5nuO0UW5P11xqvY0EURZ-_dv0aoPDjscwGfC_Ei-Nkmt-akxIT8sc6oztlsCvPPjtstkFk0KtfEb3Ba-UiQUgMu4-SLYCCr78Gfm1Dog0_Tu6ZWq-cHpz55m2xzRUhutFWCkNgn51DR-EeSTiXeQu8lCk_ju2lkL1flnHTMhuW-hK-NS61oz82VGin-qHCM-pldCjWjoDq0giCBoEiAttOTasXc6e5uRHN5Z7pXmCJHzU3rr82CGyyOc9gqPrN6gnl2q2RTCYJ3HJ6pEcPwfZar8O1RPE-BMOy9sLx7PCamtHwnsQ_gNjNvqvc4VOWTeclc47rGHqGGECxAXEtdXkPu5CUz7aXk_bkPuwr00xTNXCruPy205naCzxQ7oS2npFceTcX7lFGDcvjBQRhRMRshOWR4DSeYdyBiY6T8toqqOi4gadSV1YNWf1n7BDtjPqBktXylU7tyJPSgELBcU__ys_vGuNd21m_7cp-92hMW4AUPGwijNH35myeLWyFD6UrtQrzVCFxPsdnp6biV_yoU7QxHwWETvpquQ4tVOIhHTkzBq_om0rAXhBjSWIYSZ6AwUKbMaAhr8aofmUdUsfJjwoU2FRsgFnBmo_cKvdM6gPB8KIrSdxgY_Y79WO1oF6LMF60CFLwU7gY97FrFdg-B2Ffbr7Bi9w4VoIO4izZIKkdCpluC7Dx5P3KCcgO9EjhF6ilVCj4pD07wzzE6iC3O7ptIhxHEfzdJHpEFkrcVsteRstZly5U-KWMgB7yf0ewYhLwBgiFqAd8v3W15P3eu0Gi0QOYu56IRJ7VRQoNCG1Cexf4EYT5ameoc0dBvIs2JAHz8a8ozBvZIR_qkI5DX6Ct9BafsAgqWu-jFszrKjWYX6wMHqPguttTTvdfuqfcQEwtyJDRihnfiIjegN7pFFMp4H-uXhg9vatIt5ob1zuQTANArx8SrfN-lQO9i_PtjRUN6_VMj5casyl9RXBR4EVmJhH8_ySiXSbEvzEPs5z0TgHMmGL8GlUAoakc_7V_ejrFfRawId5cowQBt7_SqSQ6Yb7xooU7WhonLoiXe_5CVo7fd-ogiLBIjEVNh-FnyqExO81HZxPWxiLiOo3fQN-VdIZiym86sK7PJOTJ3YowIwWqZ1V3hkqpenKVZyoPI7KU0gHIwuiCcJuKMYM4sfJs6_b9dbMqenvK898RkXe3tJTBoq1XHkEWMFhqVs3z7_MtvFTbssK3WGULyHJnkuYPpUI-fN5KSUaOafHCEcZYoiL_jHyBhedUjyACaXNFTT5HjVBAbUWRqUU-me0)

-----

## 使用技術 💻

### バックエンド

  * **言語**: Java 21
  * **フレームワーク**: Spring Boot 3.x
  * **ビルドツール**: Maven
  * **データアクセス**: Spring JDBC (`JdbcTemplate`, `NamedParameterJdbcTemplate`)
  * **認証**: Spring Security, JWT (JSON Web Token)
  * **外部API連携**: Stripe (決済)

### データベース

  * **本番/開発**: MySQL 8.0
  * **テスト**: H2 Database

### インフラ・テスト

  * **コンテナ**: Docker, Docker Compose
  * **CI/CD**: GitHub Actions
  * **テストフレームワーク**: JUnit 5, Mockito, AssertJ

-----

## APIエンドポイント一覧 Endpoints

### 認証 (`/api/auth`)

| HTTPメソッド | URL | 説明 |
| :--- | :--- | :--- |
| `POST` | `/signup` | 新規ユーザー登録 |
| `POST` | `/login` | ログイン（JWT発行） |

### ユーザー (`/api/users`)

| HTTPメソッド | URL | 説明 |
| :--- | :--- | :--- |
| `GET` | `/me` | ログイン中のユーザー情報を取得 |

### 商品カタログ (公開用)

| HTTPメソッド | URL | 説明 |
| :--- | :--- | :--- |
| `GET` | `/api/products` | 商品一覧をページネーションで取得 |
| `GET` | `/api/products/{id}` | 特定の商品の詳細情報を取得 |
| `GET` | `/api/categories` | カテゴリ一覧を取得 |
| `GET` | `/api/categories/{id}/products` | 特定のカテゴリに属する商品一覧を取得 |

### カート (`/api/cart`)

| HTTPメソッド | URL | 説明 |
| :--- | :--- | :--- |
| `GET` | `/` | 自分のカート詳細を取得 |
| `POST` | `/items` | カートに商品を追加 |
| `PUT` | `/items/{cartItemId}` | カート内の商品数量を変更 |
| `DELETE` | `/items/{cartItemId}` | カートから商品を削除 |

### 注文 (`/api/orders`)

| HTTPメソッド | URL | 説明 |
| :--- | :--- | :--- |
| `POST` | `/` | カートの内容から注文を作成 |
| `POST` | `/payment` | 注文に対する決済を実行 |
| `GET` | `/` | 自分の注文履歴一覧を取得 |
| `GET` | `/{orderId}` | 特定の注文の詳細を取得 |

### 管理者用 (`/api/admin`)

| HTTPメソッド | URL | 説明 |
| :--- | :--- | :--- |
| `POST` | `/categories` | 新規カテゴリ作成 |
| `PUT` | `/categories/{id}` | カテゴリ情報更新 |
| `POST` | `/products` | 新規商品作成（SKU・在庫情報含む） |
| `PUT` | `/products/{id}` | 商品情報更新 |
| `POST` | `/products/{id}/skus` | 既存商品にSKUを追加 |
| `PUT` | `/skus/{skuId}` | SKU情報更新 |

-----

## 工夫した点

### 1\. 拡張性を考慮したデータベース設計

  * **SKU層の導入**:
    サイズや色といった商品のバリエーションを管理するための`skus`
    テーブルを導入しました。これにより、実際のECサイトで求められる複雑な在庫・価格管理、オプション料金などに対応できる、拡張性の高い設計を実現しました。

### 2\. パフォーマンスとセキュリティへの配慮

  * **N+1問題の対策**:
    商品一覧や注文詳細の取得において、複数の関連データを一度のクエリでまとめて取得するロジックを実装し、データベースへのアクセスを最小限に抑えました。
  * **排他ロックによる在庫引き当て**:
    注文確定時の在庫引き当て処理では、`SELECT ... FOR UPDATE`句を用いて在庫レコードをロックすることで、複数のユーザーが同時に注文した際の競合状態を防ぎ、データの整合性を保証しています。
  * **所有権の検証**:
    カートや注文情報を操作するAPIでは、リクエストされたデータが現在ログインしているユーザーのものであることを必ず検証し、他人のデータを不正に操作できないようにしました。

### 3\. 品質を保証する徹底したテスト

  * **テスト戦略の分離**:
    `@JdbcTest`（DAO層）、`@ExtendWith(MockitoExtension.class)`（サービス層）、`@SpringBootTest`（コントローラー層）を使い分け、各層の責務に応じた適切なテストを実装しました。
  * **CI/CDの実践**:
    GitHub Actions上の自動テストをDockerコンテナ（MySQL含む）で実行するように構築しました。これにより、開発環境とCI環境の差異をなくし、より信頼性の高いテストを実現しました。

-----

## 今後の課題

### 1\. 画像のアップロード機能

現在は商品登録の際に画像URLを指定する形ですが、AWS上で運用する場合は画像を直接アップロードできる機能を実装したいです。

### 2\.メールマガジンの配信機能

バッチ処置の練習がてらメールマガジン配信の機能を実装したいです。

### 3\. 管理者用UIの実装

管理者が注文履歴や売上、ユーザー情報などを確認できるダッシュボードを実装したいです。