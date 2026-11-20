# FreshBasket v2 — BigBasket-Inspired Grocery Delivery & Store Management

A complete, production-grade **BigBasket-inspired Grocery Delivery & Store Management Desktop Application** built with **Java 17**, **Java Swing (`JFrame`)**, **FlatLaf 3.5**, **Apache Maven**, **MySQL 8.x**, and **Google ZXing**.

## UI/UX Highlights (v2 Redesign)
- **Split-Screen Login & Sign-Up:** Authentic light-left trust panel + sleek pitch-black right login panel with 1-click Demo Customer & Demo Admin buttons.
- **BigBasket-Style Storefront:** Crisp white e-commerce canvas, custom-painted `fb freshbasket` logo, rounded search bar, `⚡ Delivery in 10 mins` pill, red circular Cart badge, and **3-Column Interactive "Shop by Category" Mega Menu** (dark charcoal + light gray + pure white columns).
- **Interactive Offer Banners:** Clickable `DEALS OF THE WEEK`, `BIG PACK BIGGER DISCOUNTS`, `COMBOS YOU CAN'T MISS`, and `THE ₹30 CORNER` cards.
- **Custom Vector-Illustrated Product Cards:** Clean white elevated cards with olive-green `XX% OFF` ribbon, custom `Graphics2D` product illustrations, Veg indicator dot, `⚡ 10 MINS` badge, and red-bordered `Add` button that morphs into a `- qty +` stepper.

## Quick Run (VS Code PowerShell)
```powershell
mysql -u root -proot -e "SOURCE database/schema.sql;"
mvn clean compile exec:java
```

## Demo Accounts
| Role | Email | Password |
| :--- | :--- | :--- |
| **Customer** | `devaki@freshbasket.com` | `devaki123` |
| **Admin** | `admin@freshbasket.com` | `admin123` |
