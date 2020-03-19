import argparse
import runpy

def main():
    parser = argparse.ArgumentParser(description='Tests a Simuflow-Generated Python file.')
    parser.add_argument('pythonfile', metavar='FILE', type=str)
    parser.add_argument('--signal-must-settle', action='append')

    args = parser.parse_args()

    globals = run_path(args.pythonfile)

    solutionStructure = globals['x']
    solutionStructure.y



if __name__ == "__main__":
    main()