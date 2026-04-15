import glob
import os
from pathlib import Path

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, accuracy_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline


DATA_DIR = Path(__file__).resolve().parents[1] / "data" / "youtube_spam"
MODEL_DIR = Path(__file__).resolve().parents[1] / "trained_models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)

MODEL_PATH = MODEL_DIR / "spam_model.joblib"


def load_dataset() -> pd.DataFrame:
    csv_files = glob.glob(str(DATA_DIR / "*.csv"))
    if not csv_files:
        raise FileNotFoundError(f"No CSV files found in {DATA_DIR}")

    frames = []
    for file_path in csv_files:
        df = pd.read_csv(file_path)
        frames.append(df)

    full_df = pd.concat(frames, ignore_index=True)
    return full_df


def main() -> None:
    df = load_dataset()

    print("Columns found:", list(df.columns))
    print("Total rows:", len(df))

    # UCI / Kaggle versions usually use CONTENT and CLASS
    if "CONTENT" not in df.columns or "CLASS" not in df.columns:
        raise ValueError("Expected columns CONTENT and CLASS in the dataset")

    df = df.dropna(subset=["CONTENT", "CLASS"]).copy()

    X = df["CONTENT"].astype(str)
    y = df["CLASS"].astype(int)

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