import pandas as pd

from sklearn.base import is_classifier
from sklearn.multioutput import MultiOutputRegressor
from sklearn.model_selection import cross_val_predict, KFold
from sklearn.impute import SimpleImputer

from sklearn2pmml import PMMLPipeline, ColumnTransformer, sklearn2pmml

from DataProcessor import load_agents_data, load_concepts_data, load_heuristics_data, load_ludemes_data


def filter_random_agents(agents_data, threshold):
    random_counts = {name: 0 for name in agents_data.drop(columns=['GameName']).columns}
    for _, row in agents_data.iterrows():
        for key, _ in random_counts.items():
            random_counts[key] += 1 if row[key] == row['Random'] else 0

    return agents_data[['GameName'] + [key for key, value in random_counts.items() if value/agents_data.shape[0] < threshold]]

def load_target_data():
    heuristics_data = load_heuristics_data()
    agents_data = load_agents_data()

    return filter_random_agents(agents_data, 0.5), heuristics_data

def load_feature_data():
    concepts_data = load_concepts_data()
    ludemes_data = load_ludemes_data()
    features = pd.merge(concepts_data, ludemes_data, on='GameName')

    return features

def create_regret_matrix(targets):
    regret_matrix = targets.drop(columns='GameName').apply(
        lambda scores: scores.max() - scores, axis=1
    )
    regret_matrix['GameName'] = targets['GameName']

    return regret_matrix

def create_classification_targets(targets):
    best_targets = pd.DataFrame(targets.drop(columns=['GameName']).idxmax(axis=1))
    best_targets['GameName'] = targets['GameName']

    return best_targets.rename(columns={0: 'PredictedBest'})

def prepare_data(targets):
    target_names = list(targets.drop(columns='GameName').columns)
    features = load_feature_data()
    data = pd.merge(targets, features, on='GameName').sample(
        frac=1, random_state=69).reset_index(drop=True)
    X = data.drop(columns=target_names)
    y = data[target_names]
    
    return X, y, data['GameName']

def make_predictions(X, y, model, game_names):
    target_names = list(y.columns)
    y = y.values.ravel() if is_classifier(model) else y
    model = model if is_classifier(model) else MultiOutputRegressor(model)
    predictions = pd.DataFrame(cross_val_predict(
        model, X.drop(columns=['GameName']), y, cv=KFold(n_splits=10)))
    predictions.columns = target_names
    if (is_classifier(model)):
        predicted_best = predictions
    else:
        predicted_best = pd.DataFrame(predictions.idxmax(axis=1))
        predicted_best = predicted_best.rename(columns={0: 'PredictedBest'})
    predicted_best['GameName'] = game_names

    return predicted_best

def calculate_regret(regret_matrix, predicted_best):
    regret_scores = []
    for _, row in predicted_best.iterrows():
        regret_scores.append(regret_matrix.loc[regret_matrix['GameName'] == row['GameName'], row['PredictedBest']].values[0])
    
    return regret_scores

def evaluate_model(targets, model):
    regret_matrix = create_regret_matrix(targets)
    targets = create_classification_targets(targets) if is_classifier(model) else targets
    X, y, game_names = prepare_data(targets)
    predicted_best = make_predictions(X, y, model, game_names)
    predicted_best['Regret'] = calculate_regret(regret_matrix, predicted_best)

    return predicted_best

def export_pmml(targets, model, file_name):
    if(is_classifier(model)):
        print('PMML export not for classifiers, Ludii hyper agents require regression models')
        return
    
    X, y, _ = prepare_data(targets)
    X = X.drop(columns=['GameName'])
    preprocessor = ColumnTransformer(
        transformers=[(
            'num', 
            PMMLPipeline(steps=[('imputer', SimpleImputer(strategy='constant', fill_value=0.0))]),
            list(X.columns)
        )]
    )
    model = MultiOutputRegressor(model)
    pipeline = PMMLPipeline([
        ('preprocessor', preprocessor),
        ('model', model)
    ])
    pipeline.fit(X, y)
    sklearn2pmml(pipeline, f"{file_name}.pmml")
    print(f"{file_name}.pmml created!")