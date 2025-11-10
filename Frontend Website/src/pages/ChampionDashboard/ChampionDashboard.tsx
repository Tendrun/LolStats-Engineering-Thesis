import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  TooltipItem
} from "chart.js";
import { Bar } from "react-chartjs-2";
import { getAllChampions } from "@/lib/api/ChampionApi";
import { useEffect, useState, useMemo } from "react";
import { ChampionStats } from "@/components/ChampionMap/ChampionMapping";

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

export default function ChampionDashboard() {
  const [champions, setChampions] = useState<ChampionStats[]>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<string[]>([]);

  const options = useMemo(() => ({
    responsive: true,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      tooltip: {
        callbacks: {
          label: function(tooltipItem: TooltipItem<'bar'>) {
            const datasetLabel = tooltipItem.dataset.label || '';
            const value = tooltipItem.parsed.y ?? 0;
            
            if (datasetLabel === "Matches (normalized)" && champions) {
              const championName = tooltipItem.label;
              const champion = champions.find((c) => c.name === championName);
              const originalMatches = champion?.totalMatchesPicked || 0;
              return `${datasetLabel}: ${originalMatches} matches`;
            }
            
            return `${datasetLabel}: ${value.toFixed(1)}%`;
          }
        }
      }
    },
    scales: { 
      y: { 
        beginAtZero: true,
        title: {
          display: true,
          text: 'Percentage / Normalized Value'
        }
      },
      x: {
        title: {
          display: true,
          text: 'Champions'
        },
        barPercentage: 1, // Increase for thicker bars (max 1)
        categoryPercentage: 1 // Increase for thicker bars (max 1)
      }
    }
  }), [champions]);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);

    getAllChampions()
      .then((data) => {
        setChampions(data);
        setSelected(data.map((c: ChampionStats) => c.name)); // select all by default
      })
      .catch((err) => {
        if (err.name !== "AbortError") {
          setError(err.message || "Failed to fetch champions");
        }
      })
      .finally(() => {
        setLoading(false);
      });

    return () => {
      controller.abort();
    };
  }, []);

  // derive chart data from champions, memoized so it only recomputes when champions change
  const chartData = useMemo(() => {
    if (!champions || champions.length === 0) {
      // fallback sample data while loading / if no data
      return {
        labels: [],
        datasets: [
          {
            label: "Brak danych",
            data: [0, 0, 0, 0, 0, 0, 0],
            backgroundColor: "rgba(0,0,0,0.1)",
            borderColor: "rgba(0,0,0,0.2)",
            borderWidth: 1
          }
        ]
      };
    }

    // Filter champions by selection
    const filtered = champions.filter((c) => selected.includes(c.name));
    const champLabels = filtered.map((c) => c.name);
    // Extract data for each metric - handle both string and number formats
    const pickRateData = filtered.map((c) => {
      const rate = typeof c.pickRate === 'string' ? parseFloat(c.pickRate.replace('%', '')) : parseFloat(String(c.pickRate));
      return isNaN(rate) ? 0 : rate;
    });
    const banRateData = filtered.map((c) => {
      const rate = typeof c.banRate === 'string' ? parseFloat(c.banRate.replace('%', '')) : parseFloat(String(c.banRate));
      return isNaN(rate) ? 0 : rate;
    });
    const winRateData = filtered.map((c) => {
      const rate = typeof c.winRate === 'string' ? parseFloat(c.winRate.replace('%', '')) : parseFloat(String(c.winRate));
      return isNaN(rate) ? 0 : rate;
    });
    const matchesData = filtered.map((c) => Number(c.totalMatchesPicked ?? 0));

    // Normalize matches data to be on a similar scale as percentages (0-100)
    const maxMatches = Math.max(...matchesData);
    const normalizedMatches = matchesData.map(matches => 
      maxMatches > 0 ? (matches / maxMatches) * 100 : 0
    );

    return {
      labels: champLabels,
      datasets: [
        {
          label: "Pick Rate (%)",
          data: pickRateData,
          backgroundColor: "rgba(54, 162, 235, 0.6)",
          borderColor: "rgb(54, 162, 235)",
          borderWidth: 1
        },
        {
          label: "Ban Rate (%)",
          data: banRateData,
          backgroundColor: "rgba(255, 99, 132, 0.6)",
          borderColor: "rgb(255, 99, 132)",
          borderWidth: 1
        },
        {
          label: "Win Rate (%)",
          data: winRateData,
          backgroundColor: "rgba(75, 192, 192, 0.6)",
          borderColor: "rgb(75, 192, 192)",
          borderWidth: 1
        },
        {
          label: "Matches (normalized)",
          data: normalizedMatches,
          backgroundColor: "rgba(255, 205, 86, 0.6)",
          borderColor: "rgb(255, 205, 86)",
          borderWidth: 1
        }
      ]
    };
  }, [champions, selected]);

  if (loading) return <div className="p-6">Loading champions…</div>;
  if (error) return <div className="p-6 text-red-600">Error: {error}</div>;

  return (
    <div className="p-6 max-w-full mx-auto">
      <h2 className="text-xl font-semibold mb-4">Champion Dashboard</h2>
      {champions && (
        <div className="mb-4 flex flex-wrap gap-2 max-h-48 overflow-y-auto border p-2 rounded bg-white/80">
          {champions.map((champ) => (
            <label key={champ.name} style={{ minWidth: 120, display: 'inline-flex', alignItems: 'center', marginRight: 8 }}>
              <input
                type="checkbox"
                checked={selected.includes(champ.name)}
                onChange={() => {
                  setSelected((prev) =>
                    prev.includes(champ.name)
                      ? prev.filter((n) => n !== champ.name)
                      : [...prev, champ.name]
                  );
                }}
              />
              <span style={{ marginLeft: 4 }}>{champ.name}</span>
            </label>
          ))}
        </div>
      )}
      <div style={{ width: 4000, height: 420 }}>
        <Bar data={chartData} options={{ ...options, maintainAspectRatio: false }} />
      </div>
    </div>
  );
}