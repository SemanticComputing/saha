package fi.seco.saha3.chat.ai;

import java.util.ArrayList;
import java.util.List;

public class SentenceLengthBias implements IWordBias
{
    private final boolean longSentences;
    
    public SentenceLengthBias(boolean longSentences)
    {
        this.longSentences = longSentences;
    }
    
    public String[] filterChoices(String[] words)
    {
        List<String> candidates = new ArrayList<String>();
        
        for (Object o : words)
        {
            String word = (String) o;
            if (this.longSentences == !word.endsWith("."))
                candidates.add(word);
        }
        
        if (candidates.isEmpty())
            return null;
        
        return candidates.toArray(new String[candidates.size()]);        
    }
    
}
