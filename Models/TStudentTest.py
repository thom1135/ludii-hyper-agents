import pandas as pd
from scipy import stats
experiment_results = pd.read_csv('data/results/GameIterationResults.csv')

agents = experiment_results.drop(columns=['game_name', 'iteration']).columns

games = experiment_results['game_name'].drop_duplicates().tolist()

ttest = pd.DataFrame(index=agents, columns=agents)
p_values = []

for a1 in range(len(agents)):
    for a2 in range(a1, len(agents)):
        agent1 = agents[a1]
        agent2 = agents[a2]
        
        if a1 == a2:
            ttest.loc[agent1, agent2] = 'N/A'
        else:
            t_stat, p_value = stats.ttest_ind(experiment_results[agent1], experiment_results[agent2], equal_var=True)
            ttest.loc[agent1, agent2] = f"T: {t_stat:.3f}\nP: {p_value:.3f}"
            ttest.loc[agent2, agent1] = f"T: {-1*t_stat:.3f}\nP: {p_value:.3f}"
            p_values.append((agent1, agent2, p_value))

# Apply Holm-Bonferroni correction
alpha = 0.05
# Sort by p-value
p_values_sorted = sorted(p_values, key=lambda x: x[2]) 
for rank, (agent1, agent2, p_value) in enumerate(p_values_sorted):
    threshold = alpha / (len(p_values_sorted) - rank)
    if p_value > threshold:
        # Adjust the p-value to indicate it is not significant
        ttest.loc[agent1, agent2] = f"T: {ttest.loc[agent1, agent2].split('\n')[0]}\nP: > {threshold:.3f}"
        ttest.loc[agent2, agent1] = f"T: {ttest.loc[agent2, agent1].split('\n')[0]}\nP: > {threshold:.3f}"

ttest.to_csv(f'analysis/TTestHolmB.csv')