# ETAP 1: Budowanie aplikacji Vue
# Używamy obrazu Node.js do instalacji zależności i odpalenia builda
FROM node:20-alpine AS builder
WORKDIR /app

# Kopiujemy package.json i instalujemy zależności (optymalizacja cache)
COPY package*.json ./
RUN npm install

# Kopiujemy resztę kodu i budujemy wersję produkcyjną (Vite wypluje pliki do folderu /dist)
COPY . .
RUN npm run build

# ETAP 2: Serwowanie plików statycznych (Nginx)
FROM nginx:alpine

# Kopiujemy zbudowane pliki statyczne z etapu 1 do domyślnego folderu Nginxa
COPY --from=builder /app/dist /usr/share/nginx/html

# (Ważne dla Vue Routera)
# Gdy korzystasz z Vue Routera w trybie 'history', bezpośrednie wejście w link inny niż główny
# zwróci błąd 404 z Nginxa. Trzeba nadpisać domyślną konfigurację.
# Opcjonalnie: skopiuj tu swój własny plik nginx.conf, jeśli potrzebujesz specjalnych reguł
# COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]