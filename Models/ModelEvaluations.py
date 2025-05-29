

import pandas as pd
from sklearn.dummy import DummyClassifier, DummyRegressor
from sklearn.ensemble import GradientBoostingRegressor, RandomForestClassifier, RandomForestRegressor
from sklearn.linear_model import LinearRegression
from sklearn.naive_bayes import GaussianNB
from sklearn.neural_network import MLPClassifier
from sklearn.svm import SVR
from sklearn.tree import DecisionTreeClassifier

from ModelProcessor import evaluate_model, load_target_data


class Model:
    def __init__(self, model, label):
        self.model = model
        self.label = label

def evaluate_models(models, targets):
    results = pd.DataFrame(columns=['Model', 'Regret'])    
    for model in models:
        print('Evaluating', model.label, '...')
        next_result = pd.DataFrame.from_dict({'Model': [model.label], 'Regret' : [evaluate_model(targets, model.model)['Regret'].mean()]})
        results = pd.concat([results, next_result], ignore_index=True)

    return results

models = [
    Model(RandomForestRegressor(random_state=69, n_estimators=150), 'Random Forrest Regressor'),
    Model(LinearRegression(), 'LinearRegression'),
    Model(GradientBoostingRegressor(random_state=69), 'Gradient Boosting Regression'),
    Model(SVR(kernel="sigmoid"), 'SVR(Sigmoid)'),
    Model(DummyRegressor(strategy="mean"), 'Dummy Regressor'),
    Model(DecisionTreeClassifier(random_state=69), 'Decision Tree Classifier'),
    Model(RandomForestClassifier(random_state=69, n_estimators=150), 'Random Forest Classifier'),
    Model(MLPClassifier(hidden_layer_sizes=(150,), activation='relu', solver='adam', max_iter=1000, random_state=69), 'MLP Classifier'),
    Model(GaussianNB(), 'Gausian NB'),
    Model(DummyClassifier(strategy="most_frequent"), 'Dummy Classifier')
]

agents, heuristics = load_target_data()

agent_results = evaluate_models(models, agents)
agent_results.to_csv('analysis/AgentModels.csv')

heuristic_results = evaluate_models(models, heuristics)
heuristic_results.to_csv('analysis/HeuristicModels.csv')