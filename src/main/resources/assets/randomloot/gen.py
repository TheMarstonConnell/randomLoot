import os
import shutil
import json


def genAll(path, digit, overrides):
    num = digit / 10

    items = os.listdir("./models/item/tools/" + path)

    x = 0 
    for item in items:

        toAdd = x / 10000

        override = {
            "threshold": round(num + toAdd, 4),
            "model": {
                "type": "minecraft:model",
                "model": "randomloot:item/tools/"+ path[2:] + "/" + str(x + 1)
            }
        }

        overrides.append(override)
        x += 1

    print(str(x) + " " + path)
    return overrides

    

    

def main():
    overrides = []
    overrides = genAll("./pickaxe", 1, overrides)
    overrides = genAll("./shovel", 2, overrides)
    overrides = genAll("./axe", 3, overrides)
    overrides = genAll("./sword", 4, overrides)

    tool = {
               "model": {
                   "type": "minecraft:range_dispatch",
                   "property": "randomloot:cosmetic",
                   "scale": 1,
                   "fallback": {
                     "type": "minecraft:model",
                     "model": "randomloot:item/tool"
                   },
                   "entries": overrides
               }
           }

    out = json.dumps(tool, indent=4)
    
    text_file = open("./items/tool.json", "w")
    text_file.write(out)
    text_file.close()

    



if __name__ == "__main__":
    main()