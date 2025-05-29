import pandas as pd

selections = pd.read_csv('data/results/PortfolioSelections.csv')
regret = pd.read_csv('analysis/AgentRegret.csv')


merge = pd.merge(selections, regret, on="Game Name")

df_grouped = merge.groupby("selected_agent")["Regret"].mean()

df_grouped.to_csv("analysis/RegretByAgent.csv")