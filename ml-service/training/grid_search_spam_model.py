import glob
from pathlib import Path

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report
from sklearn.model_selection import GridSearchCV, train_test_split
from sklearn.pipeline import Pipeline


DATA_DIR = Path(__file__).resolve().parents[1] / "data" / "youtube_spam"
MODEL_DIR = Path(__file__).resolve().parents[1] / "trained_models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)

MODEL_PATH = MODEL_DIR / "spam_model_grid_best.joblib"


def load_dataset() -> pd.DataFrame:
    csv_files = glob.glob(str(DATA_DIR / "*.csv"))

    if not csv_files:
        raise FileNotFoundError(f"No CSV files found in {DATA_DIR}")

    frames = []

    for file_path in csv_files:
        df = pd.read_csv(file_path)
        frames.append(df)

    return pd.concat(frames, ignore_index=True)


def main() -> None:
    df = load_dataset()

    print("Columns found:", list(df.columns))
    print("Total rows:", len(df))

    if "CONTENT" not in df.columns or "CLASS" not in df.columns:
        raise ValueError("Expected columns CONTENT and CLASS in the dataset")

    df = df.dropna(subset=["CONTENT", "CLASS"]).copy()

    X = df["CONTENT"].astype(str)
    y = df["CLASS"].astype(int)

    print("\nLabel distribution:")
    print(y.value_counts())

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(lowercase=True)),
        ("clf", LogisticRegression(max_iter=1000, random_state=42)),
    ])

    param_grid = {
        "tfidf__stop_words": [None, "english"],
        "tfidf__ngram_range": [(1, 1), (1, 2), (1, 3)],
        "tfidf__min_df": [1, 2, 3],
        "tfidf__max_df": [0.85, 0.95, 1.0],
        "clf__C": [0.5, 1.0, 2.0, 3.0, 5.0],
        "clf__class_weight": [None, "balanced"],
    }

    grid = GridSearchCV(
        estimator=pipeline,
        param_grid=param_grid,
        scoring="f1",
        cv=5,
        n_jobs=-1,
        verbose=2,
    )

    grid.fit(X_train, y_train)

    print("\nBest parameters:")
    print(grid.best_params_)

    print(f"\nBest cross-validation F1: {grid.best_score_:.4f}")

    best_model = grid.best_estimator_

    y_pred = best_model.predict(X_test)

    print("\nTest set classification report:")
    print(classification_report(y_test, y_pred))

    joblib.dump(best_model, MODEL_PATH)
    print(f"\nSaved best grid-search model to: {MODEL_PATH}")


if __name__ == "__main__":
    main()