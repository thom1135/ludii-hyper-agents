import pandas as pd

selected_agents = pd.read_csv('data/results/PortfolioSelections.csv')
experiment_results = pd.read_csv('data/results/GameResultsMatrix.csv')

average_scores = experiment_results.drop(columns=['Ensemble', 'Weighted Ensemble', 'Ensemble (P)', 'Weigted Ensemble (P)']).groupby("game_name").mean(numeric_only=True)
portfolio_scores = pd.DataFrame(average_scores['Portfolio'])
average_scores = average_scores.drop(columns=['Portfolio'])
portfolio_scores['Best'] =average_scores.apply(
        lambda scores: scores.max(), axis=1
    )
portfolio_scores = portfolio_scores.reset_index()

portfolio_scores['Regret'] = portfolio_scores['Best'] - portfolio_scores['Portfolio']
portfolio_scores['Regret'] = portfolio_scores['Regret'].apply(lambda score: 0 if score < 0 else score)

portfolio_scores.rename(columns={'game_name': 'Game Name'}, inplace=True)

portfolio_scores.drop(columns=['Portfolio', 'Best'])

portfolio_scores.to_csv('analysis/AgentRegret.csv', index=False)