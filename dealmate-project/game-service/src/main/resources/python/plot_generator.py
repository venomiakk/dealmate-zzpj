import sys
import json
import base64
from io import BytesIO
from datetime import datetime
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import pandas as pd

def generate_bar_plot(data):
    df = pd.DataFrame(data)
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    df['date'] = df['timestamp'].dt.date
    df['amount'] = df['amount'].astype(float)
    grouped = df.groupby('date')['amount'].sum()
    plt.figure(figsize=(10, 4))
    grouped.plot(kind='bar', color='skyblue')
    plt.title('Suma punktów na dzień')
    plt.xlabel('Data')
    plt.ylabel('Suma punktów')
    plt.tight_layout()
    buf = BytesIO()
    plt.savefig(buf, format='png')
    buf.seek(0)
    img_bytes = buf.read()
    base64_str = base64.b64encode(img_bytes).decode('utf-8')
    plt.close()
    return base64_str

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Missing input file path", file=sys.stderr)
        sys.exit(1)

    input_path = sys.argv[1]
    with open(input_path, "r") as f:
        game_history = json.load(f)

    result = generate_bar_plot(game_history)
    print(result)