import pandas as pd
import numpy as np

experiment_results = pd.read_csv('data/results/GameResultsFull.csv')

results_data = pd.DataFrame(experiment_results)

# Replace agent names
results_data['score'] = results_data['score'] * 100
results_data['agent_name'] = results_data['agent_name'].replace('Ensemble -1', 'Weighted Ensemble (P)')
results_data['agent_name'] = results_data['agent_name'].replace('Ensemble -2', 'Ensemble (P)')
results_data['agent_name'] = results_data['agent_name'].replace('Ensemble 0', 'Weighted Ensemble')

# Get mean scores and error
agent_scores = results_data.groupby('agent_name').agg(
    mean_score=('score', 'mean'),
    err=('score', lambda x: np.std(x, ddof=1) / np.sqrt(len(x)))
).reset_index()

agent_scores = agent_scores.sort_values(by='mean_score', ascending=False)

agent_scores.to_csv(f'analysis/AgentScores.csv')