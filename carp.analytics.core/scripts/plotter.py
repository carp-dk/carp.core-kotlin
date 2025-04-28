import sys
import json
import argparse
import matplotlib.pyplot as plt

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", help="Optional output file path", default="output/mean_daily_steps.png")
    parser.add_argument("--input", help="Optional input file path")
    args = parser.parse_args()

    # Read input
    if args.input:
        print(f"Reading input from file: {args.input}")
        with open(args.input, "r") as f:
            input_data = f.read()
    else:
        print("Reading input from stdin...")
        input_data = sys.stdin.read()

    records = json.loads(input_data)

    subject_ids = [record["subject_id"] for record in records]
    mean_steps = [int(record.get("steps", 0)) for record in records]

    plt.figure(figsize=(10,6))
    plt.scatter(subject_ids, mean_steps)
    plt.xlabel("Subject ID")
    plt.ylabel("Mean Daily Steps")
    plt.title("Mean Daily Step Counts per Subject")
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()

    # Make sure directory exists
    import os
    output_dir = os.path.dirname(args.output)
    if output_dir and not os.path.exists(output_dir):
        os.makedirs(output_dir)

    plt.savefig(args.output)
    print(f"Plot saved to {args.output}")

if __name__ == "__main__":
    main()
