import type { RateUpdateDay } from "../../types/rateUpdates";
import { RateUpdatesDay } from "./RateUpdatesDay";

export function RateUpdatesList({ days }: { days: RateUpdateDay[] }) {
    return (
        <div className="flex flex-col gap-4">
            {days.map(day => (
                <RateUpdatesDay key={day.date} day={day} />
            ))}
        </div>
    );
}