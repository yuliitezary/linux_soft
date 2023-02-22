package launchserver.auth.limiter;

public class AuthEntry
{
    public int value;
    public long ts;

    public AuthEntry(int i, long l)
    {
        value = i;
        ts = l;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }

        if (!(obj instanceof AuthEntry))
        {
            return false;
        }

        AuthEntry other = (AuthEntry) obj;
        if (ts != other.ts)
        {
            return false;
        }

        return value == other.value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (ts ^ ts >>> 32);
        result = prime * result + value;
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("AuthEntry {value=%s, ts=%s}", value, ts);
    }
}
