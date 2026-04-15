import json
from pathlib import Path

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, accuracy_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline


DATA_DIR = Path(__file__).resolve().parents[1] / "data" / "troll_comments"
MODEL_DIR = Path(__file__).resolve().parents[1] / "trained_models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)

MODEL_PATH = MODEL_DIR / "troll_model.joblib"


def load_jsonl_dataset() -> pd.DataFrame:
    json_files = list(DATA_DIR.glob("*.json")) + list(DATA_DIR.glob("*.jsonl"))
    if not json_files:
        raise FileNotFoundError(f"No JSON/JSONL files found in {DATA_DIR}")

    rows = []

    for file_path in json_files:
        with open(file_path, "r", encoding="utf-8") as f:
            for line_number, line in enumerate(f, start=1):
                line = line.strip()
                if not line:
                    continue

                try:
                    obj = json.loads(line)
                except json.JSONDecodeError as e:
                    print(f"Skipping invalid JSON at {file_path}:{line_number} -> {e}")
                    continue

                content = obj.get("content", "")
                annotation = obj.get("annotation", {})
                labels = annotation.get("label", [])

                if not content or not labels:
                    continue

                label = labels[0]

                rows.append({
                    "content": str(content),
                    "label": int(label)
                })

    if not rows:
        raise ValueError("No valid rows found in troll dataset")

    return pd.DataFrame(rows)


def main() -> None:
    df = load_jsonl_dataset()

    print("Columns found:", list(df.columns))
    print("Total rows:", len(df))
    print("Label distribution:")
    print(df["label"].value_counts())

    X = df["content"].astype(str)
    y = df["label"].astype(int)

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y
    )

    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(
            lowercase=True,
            stop_words="english",
            ngram_range=(1, 2),
            min_df=2
        )),
        ("clf", LogisticRegression(
            max_iter=1000,
            random_state=42
        ))
    ])

    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_test)

    acc = accuracy_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)

    print(f"Accuracy: {acc:.4f}")
    print(f"F1 Score: {f1:.4f}")
    print("\nClassification report:")
    print(classification_report(y_test, y_pred))

    joblib.dump(pipeline, MODEL_PATH)
    print(f"\nSaved model to: {MODEL_PATH}")


if __name__ == "__main__":
    main()