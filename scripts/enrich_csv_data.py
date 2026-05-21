import csv
import time
import random
import requests
from bs4 import BeautifulSoup
import sys
import os

# Requirements: pip install requests beautifulsoup4

INPUT_FILE = 'data/amazon_bestsellers_full.csv'
OUTPUT_FILE = 'data/amazon_bestsellers_enriched.csv'

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Accept-Language': 'en-US,en;q=0.9',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
    'Accept-Encoding': 'gzip, deflate, br',
    'Connection': 'keep-alive',
    'Upgrade-Insecure-Requests': '1'
}

def enrich_data():
    print("="*80)
    print("🚀 ENRICHISSEMENT DES DONNÉES AMAZON")
    print("="*80)
    print(f"📁 Fichier source: {INPUT_FILE}")
    print(f"📁 Fichier destination: {OUTPUT_FILE}")
    print()
    
    # Check if input file exists
    if not os.path.exists(INPUT_FILE):
        print(f"❌ Erreur: Fichier {INPUT_FILE} introuvable!")
        return
    
    # Read input file
    print(f"📖 Lecture de {INPUT_FILE}...")
    try:
        with open(INPUT_FILE, 'r', encoding='utf-8', errors='replace') as f:
            reader = csv.reader(f)
            rows = list(reader)
    except Exception as e:
        print(f"❌ Erreur lors de la lecture: {e}")
        return

    if not rows:
        print("❌ Fichier vide!")
        return

    header = rows[0]
    
    # Add new headers if not exists
    if "Product Name" not in header:
        header.append("Product Name")
    if "Image URL" not in header:
        header.append("Image URL")
    
    # Get column indices
    try:
        link_idx = header.index("Product Link")
        name_idx = header.index("Product Name")
        img_idx = header.index("Image URL")
    except ValueError as e:
        print(f"❌ Erreur: Colonne manquante - {e}")
        return

    # Load existing enriched data if available (for resume)
    enriched_rows = [header]
    start_from = 1
    
    if os.path.exists(OUTPUT_FILE):
        print(f"📂 Fichier enrichi existant trouvé, chargement pour reprise...")
        try:
            with open(OUTPUT_FILE, 'r', encoding='utf-8', errors='replace') as f:
                reader = csv.reader(f)
                existing = list(reader)
                if len(existing) > 1:
                    enriched_rows = existing
                    start_from = len(existing)
                    print(f"✅ Reprise depuis la ligne {start_from}")
        except Exception as e:
            print(f"⚠️  Impossible de charger le fichier existant: {e}")
    
    total = len(rows) - 1
    enriched_count = 0
    skipped_count = 0
    error_count = 0
    
    print()
    print(f"📊 Total de produits: {total}")
    print(f"📊 Déjà enrichis: {start_from - 1}")
    print(f"📊 Restants: {total - start_from + 1}")
    print()
    print("⏳ Démarrage de l'enrichissement...")
    print("   (Appuyez sur Ctrl+C pour arrêter et sauvegarder)")
    print()
    
    try:
        for i in range(start_from, len(rows)):
            row = rows[i]
            
            # Ensure row has enough columns
            while len(row) < len(header):
                row.append("")
            
            url = row[link_idx]
            current_name = row[name_idx] if len(row) > name_idx else ""
            current_img = row[img_idx] if len(row) > img_idx else ""
            
            # Skip if already enriched
            if current_name and current_img and not current_name.startswith("Electronics Best") and not current_name.startswith("Books Best"):
                enriched_rows.append(row)
                skipped_count += 1
                if i % 50 == 0:
                    print(f"⏭️  [{i}/{total}] Déjà enrichi, ignoré")
                continue
            
            if url:
                print(f"🔍 [{i}/{total}] Enrichissement en cours...")
                try:
                    # Random delay to avoid rate limiting
                    delay = random.uniform(2, 5)
                    time.sleep(delay)
                    
                    response = requests.get(url, headers=HEADERS, timeout=15)
                    
                    if response.status_code == 200:
                        soup = BeautifulSoup(response.content, 'html.parser')
                        
                        # Extract title
                        title_tag = soup.find("span", {"id": "productTitle"})
                        if not title_tag:
                            title_tag = soup.find("h1", {"id": "title"})
                        title = title_tag.get_text().strip() if title_tag else ""
                        
                        # Extract image
                        img_tag = soup.find("img", {"id": "landingImage"})
                        if not img_tag:
                            img_tag = soup.find("img", {"class": "a-dynamic-image"})
                        img_url = img_tag.get('src') if img_tag else ""
                        
                        if title:
                            row[name_idx] = title
                            row[img_idx] = img_url
                            enriched_count += 1
                            print(f"   ✅ Trouvé: {title[:60]}...")
                        else:
                            print(f"   ⚠️  Titre non trouvé")
                            error_count += 1
                            
                    elif response.status_code == 503:
                        print(f"   ⚠️  Service temporairement indisponible (503)")
                        print(f"   ⏸️  Pause de 30 secondes...")
                        time.sleep(30)
                        error_count += 1
                    else:
                        print(f"   ❌ Échec (Status: {response.status_code})")
                        error_count += 1
                        
                except KeyboardInterrupt:
                    print("\n\n⏸️  Interruption par l'utilisateur...")
                    raise
                except Exception as e:
                    print(f"   ❌ Erreur: {str(e)[:50]}")
                    error_count += 1
            
            enriched_rows.append(row)
            
            # Save progress every 10 products
            if i % 10 == 0:
                with open(OUTPUT_FILE, 'w', newline='', encoding='utf-8') as f:
                    writer = csv.writer(f)
                    writer.writerows(enriched_rows)
                print(f"   💾 Progression sauvegardée ({enriched_count} enrichis, {error_count} erreurs)")
    
    except KeyboardInterrupt:
        print("\n\n⏸️  Arrêt demandé par l'utilisateur")
    
    # Final save
    print("\n💾 Sauvegarde finale...")
    with open(OUTPUT_FILE, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerows(enriched_rows)
    
    print()
    print("="*80)
    print("✅ ENRICHISSEMENT TERMINÉ")
    print("="*80)
    print(f"📊 Statistiques:")
    print(f"   • Total traité: {len(enriched_rows) - 1}")
    print(f"   • Nouveaux enrichis: {enriched_count}")
    print(f"   • Déjà enrichis: {skipped_count}")
    print(f"   • Erreurs: {error_count}")
    print(f"   • Fichier: {OUTPUT_FILE}")
    print("="*80)

if __name__ == "__main__":
    enrich_data()

