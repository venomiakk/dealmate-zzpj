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
    colors = ['green' if val >= 0 else 'red' for val in grouped.values]
    plt.figure(figsize=(10, 4))
    bars = plt.bar(grouped.index.astype(str), grouped.values, color=colors)
    plt.axhline(0, color='black', linestyle='--', linewidth=1)
    plt.title('Credits per Day')
    plt.tight_layout()
    # Add value labels above bars
    for bar in bars:
        height = bar.get_height()
        plt.text(
            bar.get_x() + bar.get_width() / 2,
            height + (0.01 if height >= 0 else -0.01),
            f'{height:.2f}',
            ha='center',
            va='bottom' if height >= 0 else 'top',
            fontsize=8
        )
    buf = BytesIO()
    plt.savefig(buf, format='png', dpi=300)
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