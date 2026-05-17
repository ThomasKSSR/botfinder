import json
from pathlib import Path

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    f1_score,
    precision_score,
    recall_score,
)
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline


DATA_DIR = Path(__file__).resolve().parents[1] / "data" / "troll_comments"


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

                try:
                    label = int(labels[0])
                except ValueError:
                    continue

                rows.append({
                    "content": str(content),
                    "label": label,
                })

    if not rows:
        raise ValueError("No valid rows found in troll dataset")

    return pd.DataFrame(rows)


def evaluate_config(name, X_train, X_test, y_train, y_test, tfidf_params, clf_params):
    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(**tfidf_params)),
        ("clf", LogisticRegression(**clf_params)),
    ])

    pipeline.fit(X_train, y_train)
    y_pred = pipeline.predict(X_test)

    return {
        "name": name,
        "accuracy": accuracy_score(y_test, y_pred),
        "f1": f1_score(y_test, y_pred),
        "precision": precision_score(y_test, y_pred),
        "recall": recall_score(y_test, y_pred),
        "tfidf_params": tfidf_params,
        "clf_params": clf_params,
        "pipeline": pipeline,
    }


def evaluate_thresholds(model, X_test, y_test):
    probabilities = model.predict_proba(X_test)[:, 1]

    rows = []

    for threshold in [0.30, 0.35, 0.40, 0.45, 0.50, 0.55, 0.60, 0.65, 0.70]:
        y_pred = (probabilities >= threshold).astype(int)

        rows.append({
            "threshold": threshold,
            "accuracy": accuracy_score(y_test, y_pred),
            "f1": f1_score(y_test, y_pred),
            "precision": precision_score(y_test, y_pred),
            "recall": recall_score(y_test, y_pred),
        })

    return pd.DataFrame(rows)


def main() -> None:
    df = load_jsonl_dataset()

    print("Columns found:", list(df.columns))
    print("Total rows:", len(df))

    df = df.dropna(subset=["content", "label"]).copy()

    X = df["content"].astype(str)
    y = df["label"].astype(int)

    print("\nLabel distribution:")
    print(y.value_counts())

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    experiments = [
        {
            "name": "baseline_stopwords_bigram",
            "tfidf": {
                "lowercase": True,
                "stop_words": "english",
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
            },
        },
        {
            "name": "no_stopwords_bigram",
            "tfidf": {
                "lowercase": True,
                "stop_words": None,
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
            },
        },
        {
            "name": "balanced_no_stopwords_bigram",
            "tfidf": {
                "lowercase": True,
                "stop_words": None,
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
                "class_weight": "balanced",
            },
        },
        {
            "name": "trigrams_no_stopwords",
            "tfidf": {
                "lowercase": True,
                "stop_words": None,
                "ngram_range": (1, 3),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
            },
        },
        {
            "name": "min_df_1_no_stopwords",
            "tfidf": {
                "lowercase": True,
                "stop_words": None,
                "ngram_range": (1, 2),
                "min_df": 1,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
            },
        },
        {
            "name": "C_0_5_no_stopwords",
            "tfidf": {
                "lowercase": True,
                "stop_words": None,
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
                "C": 0.5,
            },
        },
        {
            "name": "C_2_no_stopwords",
            "tfidf": {
                "lowercase": True,
                "stop_words": None,
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
                "C": 2.0,
            },
        },
        {
            "name": "balanced_C_2_no_stopwords",
            "tfidf": {
                "lowercase": True,
                "stop_words": None,
                "ngram_range": (1, 2),
                "min_df": 2,
            },
            "clf": {
                "max_iter": 1000,
                "random_state": 42,
                "C": 2.0,
                "class_weight": "balanced",
            },
        },
    ]

    results = []
    best_result = None

    for config in experiments:
        print("\n" + "=" * 80)
        print(f"Running experiment: {config['name']}")

        result = evaluate_config(
            config["name"],
            X_train,
            X_test,
            y_train,
            y_test,
            config["tfidf"],
            config["clf"],
        )

        results.append(result)

        print(f"Accuracy:  {result['accuracy']:.4f}")
        print(f"F1:        {result['f1']:.4f}")
        print(f"Precision: {result['precision']:.4f}")
        print(f"Recall:    {result['recall']:.4f}")

        if best_result is None or result["f1"] > best_result["f1"]:
            best_result = result

    summary = pd.DataFrame([
        {
            "name": r["name"],
            "accuracy": r["accuracy"],
            "f1": r["f1"],
            "precision": r["precision"],
            "recall": r["recall"],
        }
        for r in results
    ]).sort_values(by="f1", ascending=False)

    print("\n" + "=" * 80)
    print("SUMMARY")
    print(summary.to_string(index=False))

    print("\n" + "=" * 80)
    print("BEST MODEL")
    print(best_result["name"])
    print(f"Best F1: {best_result['f1']:.4f}")

    y_pred = best_result["pipeline"].predict(X_test)

    print("\nClassification report for best model:")
    print(classification_report(y_test, y_pred))

    print("\nThreshold comparison for best model:")
    threshold_df = evaluate_thresholds(best_result["pipeline"], X_test, y_test)
    print(threshold_df.to_string(index=False))


if __name__ == "__main__":
    main()