import pandas as pd
import matplotlib.pyplot as plt

model_results = pd.read_csv('analysis/HeuristicModels.csv')
df = pd.DataFrame(model_results)

plt.barh(model_results['Model'], model_results['Regret'])

plt.xlabel('Average Regret')
plt.ylabel('Model')
plt.grid(axis='x')
plt.tight_layout()

plt.show()