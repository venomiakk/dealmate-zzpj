import sys
import json
import base64

from io import BytesIO
from datetime import datetime
import matplotlib
matplotlib.use('Agg') # Ustaw backend PRZED importem pyplot
import matplotlib.pyplot as plt
import matplotlib.dates as mdates

def generate_plot(data):
    # Przekształcenie timestampów i amountów
    timestamps = [datetime.fromisoformat(entry["timestamp"]) for entry in data]
    amounts = [float(entry["amount"]) for entry in data]

    plt.figure(figsize=(8, 4))
    plt.plot(timestamps, amounts, marker='o', linestyle='-', color='blue', label='Score')
    plt.title("Game Score Over Time")
    plt.xlabel("Time")
    plt.ylabel("Score")
    plt.gcf().autofmt_xdate()
    plt.grid(True)
    plt.legend()

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

    result = generate_plot(game_history)
    print(result)
